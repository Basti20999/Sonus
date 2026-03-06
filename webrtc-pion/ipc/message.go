package ipc

import (
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/buffer"
)

type IpcMessage interface {
	Decode(buf *buffer.ByteBuf) error

	Encode(buf *buffer.ByteBuf) error

	Handle(handler *Handler) error

	GetHandlerId() uint32
}
