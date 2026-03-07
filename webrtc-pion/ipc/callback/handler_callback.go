package callback

import (
	"log"
	"sync"
	"time"

	"github.com/pion/webrtc/v4"
	"minceraft.dev/sonus/webrtc-pion/ipc"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/commonbound"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/sonusbound"
	"minceraft.dev/sonus/webrtc-pion/pion"
)

type HandlerCallback struct {
	*ipc.Handler
	trackCounterLock sync.Mutex
	trackCounter     uint32
}

func (handler *HandlerCallback) OnIceCandidate(candidate string, sdpMid *string, sdpMLineIndex *uint16) error {
	return handler.Send(&commonbound.IpcPeerAddIceCandidate{
		HandlerId:     handler.Id,
		Candidate:     candidate,
		SdpMid:        sdpMid,
		SdpMLineIndex: sdpMLineIndex,
	})
}

func (handler *HandlerCallback) OnIceConnectionStateChange(state webrtc.ICEConnectionState) error {
	return handler.Send(&sonusbound.IpcPeerOnIceConnectionStateChange{
		HandlerId: handler.Id,
		State:     state,
	})
}

func (handler *HandlerCallback) OnConnectionStateChange(state webrtc.PeerConnectionState) error {
	return handler.Send(&sonusbound.IpcPeerOnConnectionStateChange{
		HandlerId: handler.Id,
		State:     state,
	})
}

func (handler *HandlerCallback) GenerateTrackId() uint32 {
	handler.trackCounterLock.Lock()
	defer handler.trackCounterLock.Unlock()

	trackId := handler.trackCounter
	handler.trackCounter = trackId + 1
	return trackId
}

func (handler *HandlerCallback) OnRemoteAudioTrack(sampleRate uint32, channels uint16) (pion.PionTrackCallback, error) {
	trackId := handler.GenerateTrackId()

	// inform sonus about new track
	if err := handler.Send(&sonusbound.IpcPeerOnAudioTrack{
		HandlerId:  handler.Id,
		Type:       sonusbound.AudioTrackTypeRemote,
		TrackId:    trackId,
		SampleRate: sampleRate,
		Channels:   channels,
	}); err != nil {
		return nil, err
	}

	// handle track data
	return func(data []byte, duration time.Duration) {
		if writeErr := handler.Send(&sonusbound.IpcRemoteTrackOnData{
			HandlerId: handler.Id,
			TrackId:   trackId,
			Data:      data,
			Duration:  duration,
		}); writeErr != nil {
			handler.OnError(writeErr)
		}
	}, nil
}

func (handler *HandlerCallback) OnLocalAudioTrack(sampleRate uint32, channels uint16, callback pion.PionTrackCallback) error {
	trackId := handler.GenerateTrackId()

	// inform sonus about new track
	if err := handler.Send(&sonusbound.IpcPeerOnAudioTrack{
		HandlerId:  handler.Id,
		Type:       sonusbound.AudioTrackTypeLocal,
		TrackId:    trackId,
		SampleRate: sampleRate,
		Channels:   channels,
	}); err != nil {
		return err
	}

	// save callback, will be used by sonus to push audio frames
	handler.LocalTrackMap.Store(trackId, callback)
	return nil
}

func (handler *HandlerCallback) OnError(err error) {
	writeErr := handler.Send(&sonusbound.IpcPeerError{
		HandlerId: handler.Id,
		Error:     err.Error(),
	})
	if writeErr != nil {
		log.Printf("failed to send error %e to %s: %e", err, handler.Peer.String(), writeErr)
	}
}
