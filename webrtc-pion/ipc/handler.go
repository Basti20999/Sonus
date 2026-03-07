package ipc

import (
	"minceraft.dev/sonus/webrtc-pion/pion"
)

type Handler struct {
	Id     uint32
	Socket *SocketConn
	Peer   *pion.PionPeer
}
