package ipc

import (
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/buffer"
)

type Message interface {
	GetPionboundId() uint8

	GetSonusboundId() uint8

	Decode(buf *buffer.ByteBuf) error

	Encode(buf *buffer.ByteBuf) error

	Handle(handler *Handler) error

	GetHandlerId() uint32
}
