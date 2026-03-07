package ipc

import (
	"fmt"
	"sync"
	"time"

	"minceraft.dev/sonus/webrtc-pion/pion"
)

type Handler struct {
	Id            uint32
	Socket        *SocketConn
	Peer          *pion.PionPeer
	LocalTrackMap sync.Map
}

func (handler *Handler) SendLocalAudio(trackId uint32, frame []byte, duration time.Duration) error {
	callback, ok := handler.LocalTrackMap.Load(trackId)
	if !ok {
		return fmt.Errorf("tried sending audio to unknown track %d", trackId)
	}
	callback.(pion.PionTrackCallback)(frame, duration)
	return nil
}
