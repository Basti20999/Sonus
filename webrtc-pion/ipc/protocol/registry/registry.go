package registry

import (
	"errors"
	"reflect"

	"minceraft.dev/sonus/webrtc-pion/ipc"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/buffer"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/commonbound"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/pionbound"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/sonusbound"
)

var (
	inboundMessages = buildInboundMessages([]ipc.Message{
		// commonbound
		&commonbound.IpcPeerAddIceCandidate{},
		&commonbound.IpcPeerSdp{},
		// pionbound
		&pionbound.IpcApiAllocatePeer{},
		&pionbound.IpcLocalTrackSendData{},
		&pionbound.IpcPeerClose{},
		// sonusbound
		&sonusbound.IpcPeerError{},
		&sonusbound.IpcPeerOnAudioTrack{},
		&sonusbound.IpcPeerOnConnectionStateChange{},
		&sonusbound.IpcPeerOnIceConnectionStateChange{},
		&sonusbound.IpcRemoteTrackOnData{},
	})
	inboundMessageCount = uint32(len(inboundMessages))
)

type messageDecoder struct {
	message ipc.Message
}

func (decoder messageDecoder) decode(buf *buffer.ByteBuf) (msg ipc.Message, err error) {
	msg = reflect.ValueOf(decoder.message).Elem().Addr().Interface().(ipc.Message)
	err = msg.Decode(buf)
	return
}

func buildInboundMessages(allMessages []ipc.Message) []messageDecoder {
	decoders := make([]messageDecoder, 0)
	for _, message := range allMessages {
		decoders = append(decoders, messageDecoder{message: message})
	}
	return decoders
}

var (
	ErrorMessageNotEncodable = errors.New("message is not encodable")
	ErrorUnknownMessageId    = errors.New("unknown message id")
)

func Encode(message ipc.Message) (*buffer.ByteBuf, error) {
	id := message.GetSonusboundId()
	if id == 0xFF {
		return nil, ErrorMessageNotEncodable
	}
	// allocate buffer
	buf := buffer.AllocatePooled(buffer.DefaultInitialCapacity)
	defer buf.Release()

	// write id and encode message
	buf.WriteVarInt(uint32(id))
	if err := message.Encode(buf); err != nil {
		return nil, err
	}
	// return retained buffer
	return buf.Retain(), nil
}

func Decode(buf *buffer.ByteBuf) (ipc.Message, error) {
	id, err := buf.ReadVarInt()
	if err != nil {
		return nil, err
	} else if id >= inboundMessageCount {
		return nil, ErrorUnknownMessageId
	}
	// decode message content
	ret, err := inboundMessages[id].decode(buf)
	if err != nil {
		return nil, err
	}
	// check we fully this message was fully read
	if buf.IsReadable(1) {
		return nil, fmt.Errorf("still %d bytes left in buffer after reading %e", buf.ReadableBytes(), ret)
	}
	return ret, nil
}
