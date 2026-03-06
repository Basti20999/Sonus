package pionbound

import (
	"github.com/pion/webrtc/v4"
	"minceraft.dev/sonus/webrtc-pion/ipc"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/buffer"
)

type IcpApiAllocatePeer struct {
	HandlerId    uint32
	IceServers   []webrtc.ICEServer
	BundlePolicy webrtc.BundlePolicy
	Id           string
}

func (msg *IcpApiAllocatePeer) Decode(buf *buffer.ByteBuf) (err error) {
	if msg.HandlerId, err = buf.ReadVarInt(); err != nil {
		return
	} else if msg.IceServers, err = buffer.ReadSlice(buf, func() (ret webrtc.ICEServer, err error) {
		var url string
		var user *string
		var auth *string
		if url, err = buf.ReadUtf8(); err != nil {
			return
		} else if user, err = buffer.ReadNilable(buf, buf.ReadUtf8); err != nil {
			return
		} else if auth, err = buffer.ReadNilable(buf, buf.ReadUtf8); err != nil {
			return
		}
		ret.URLs = []string{url}
		return
	}); err != nil {
		return
	}
	return
}

func (msg *IcpApiAllocatePeer) Encode(*buffer.ByteBuf) error {
	panic("unsupported")
}

func (msg *IcpApiAllocatePeer) Handle(handler *ipc.Handler) error {
}

func (msg *IcpApiAllocatePeer) GetHandlerId() uint32 {
	return msg.HandlerId
}
