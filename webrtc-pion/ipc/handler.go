package ipc

import (
	"fmt"
	"sync"
	"time"

	"minceraft.dev/sonus/webrtc-pion/pion"
)

type Handler struct {
	Id            uint32
	Peer          *pion.PionPeer
	LocalTrackMap sync.Map
	Send          func(msg Message) error
	Close         func() error
}

func (handler *Handler) SendLocalAudio(trackId uint32, frame []byte, duration time.Duration) error {
	callback, ok := handler.LocalTrackMap.Load(trackId)
	if !ok {
		return fmt.Errorf("tried sending audio to unknown track %d", trackId)
	}
	callback.(pion.PionTrackCallback)(frame, duration)
	return nil
}
