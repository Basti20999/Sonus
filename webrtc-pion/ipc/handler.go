package ipc

import "minceraft.dev/sonus/webrtc-pion/pion"

type Handler struct {
	Socket *Socket
	Peer   *pion.PionPeer
}
