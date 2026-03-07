package socket

import (
	"log"
	"net"
	"os"
	"os/signal"
	"sync"
	"syscall"

	"minceraft.dev/sonus/webrtc-pion/ipc"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/buffer"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/registry"
)

type SocketConn struct {
	conn      net.Conn
	writeLock sync.Mutex
	Handlers  map[uint32]*ipc.Handler
}

func BindSocket(path string) error {
	// listen on path for IPC connections
	listener, err := net.Listen("unix", path)
	if err != nil {
		return err
	}

	running := true
	// properly shutdown unix socket on termination
	// https://stackoverflow.com/a/16702173
	exitSignalCh := make(chan os.Signal, 1)
	signal.Notify(exitSignalCh, os.Interrupt, os.Kill, syscall.SIGTERM)
	go func(ch chan os.Signal) {
		exitSignal := <-ch
		log.Printf("shutting down, received %s", exitSignal)
		// graceful shutdown
		running = false
		_ = listener.Close()
		os.Exit(0)
	}(exitSignalCh)

	// start loop
	var conn net.Conn
	for running {
		conn, err = listener.Accept()
		if err != nil {
			if running {
				log.Printf("error while accepting connection: %s", err)
			}
			continue // don't abort
		}
		go handleSocket(conn)
	}

	return nil // done executing
}

// gcMessageInterval determines when we remove read bytes from the socket read buffer
const gcMessageInterval = uint8(15)

func handleSocket(conn net.Conn) {
	log.Printf("accepted connection from %s", conn.RemoteAddr())
	socketConn := SocketConn{
		conn:     conn,
		Handlers: make(map[uint32]*ipc.Handler),
	}
	bufGcTick := gcMessageInterval

	var msg ipc.Message
	var err error
	// pooling is useless here
	buf := buffer.AllocateUnpooled(buffer.DefaultInitialCapacity * 2)
	for {
		// periodically discard some bytes from the buffer which have already been read + handled
		bufGcTick--
		if bufGcTick == 0 {
			bufGcTick = gcMessageInterval
			buf.DiscardSomeReadBytes(buffer.DefaultInitialCapacity)
		}

		// ensure there is always enough space in buffer
		buf.EnsureWritable(buffer.DefaultInitialCapacity)
		// read bytes from socket directly into bytebuf
		if err = buf.ReadFrom(conn); err != nil {
			log.Printf("error while reading from %s: %s", conn.RemoteAddr(), err)
			_ = conn.Close()
			return
		}
		// attempt reading frame length
		ri := buf.GetReaderIndex()
		var frameLen uint32
		frameLen, err = buf.ReadVarInt()
		if err != nil || !buf.IsReadable(frameLen) {
			// either can't read full varint yet or frame hasn't fully arrived yet, wait for more data to arrive
			_ = buf.SetReaderIndex(ri)
			continue
		}
		// read frame slice; ignore error, we checked this before
		frame, _ := buf.ReadUnsafeSlice(frameLen)
		// decode frame to an ipc message
		if msg, err = registry.Decode(frame); err != nil {
			log.Printf("error while decoding message from %s: %s", conn.RemoteAddr(), err)
			continue // trusted connection, don't close
		}

		// create handler if not exists
		handlerId := msg.GetHandlerId()
		handler, exists := socketConn.Handlers[handlerId]
		if !exists {
			handler = &ipc.Handler{
				Id:   handlerId,
				Send: socketConn.Send,
				Close: func() error {
					delete(socketConn.Handlers, handlerId)
					return handler.Peer.Close()
				},
			}
			socketConn.Handlers[handlerId] = handler
		}

		// handle message using handler and handle error
		if err = msg.Handle(handler); err != nil {
			log.Printf("error while handling message from %s: %s", conn.RemoteAddr(), err)
		}
	}
}

func (socket *SocketConn) Send(msg ipc.Message) error {
	// encode ipc message to bytes
	contentBuf, err := registry.Encode(msg)
	if err != nil {
		return err
	}
	defer contentBuf.Release()

	// write var int to separate buffer to prevent copying the content buffer
	lengthBuf := buffer.AllocatePooled(5)
	defer lengthBuf.Release()
	lengthBuf.WriteVarInt(contentBuf.ReadableBytes())

	// lock to prevent some other thread from interfering with our message
	socket.writeLock.Lock()
	defer socket.writeLock.Unlock()

	// write length + content to connection
	if _, err = socket.conn.Write(lengthBuf.UnsafeArraySlice()); err != nil {
		return err
	} else if _, err = socket.conn.Write(contentBuf.UnsafeArraySlice()); err != nil {
		return err
	}

	// done sending!
	return nil
}
