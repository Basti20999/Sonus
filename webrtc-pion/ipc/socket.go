package ipc

import (
	"fmt"
	"net"
	"os"
)

type SocketConn struct {
	conn     net.Conn
	Handlers map[uint32]*Handler
}

func BindSocket(path string) error {
	listener, err := net.Listen("unix", path)
	if err != nil {
		return err
	}
	var conn net.Conn
	for {
		conn, err = listener.Accept()
		if err != nil {
			_, _ = fmt.Fprintf(os.Stderr, "error while accepting connection: %e", err)
			continue // don't abort
		}
		go handleSocket(conn)
	}
}

func handleSocket(conn net.Conn) {
	conn.Read()
}

func (socket *SocketConn) Send(msg Message) error {
	msg.Encode()
	return nil
}
