package callback

import (
	"github.com/pion/webrtc/v4"
	"minceraft.dev/sonus/webrtc-pion/ipc"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/commonbound"
	"minceraft.dev/sonus/webrtc-pion/pion"
)

type HandlerCallback struct {
	*ipc.Handler
}

func (handler *HandlerCallback) OnIceCandidate(candidate string, sdpMid *string, sdpMLineIndex *uint16) error {
	return handler.Socket.Send(&commonbound.IcpPeerAddIceCandidate{
		HandlerId:     handler.Id,
		Candidate:     candidate,
		SdpMid:        sdpMid,
		SdpMLineIndex: sdpMLineIndex,
	})
}

func (handler *HandlerCallback) OnIceConnectionStateChange(state webrtc.ICEConnectionState) error {
	//return handler.Socket.Send()
}

func (handler *HandlerCallback) OnConnectionStateChange(state webrtc.PeerConnectionState) error {
	//return handler.Socket.Send()
}

func (handler *HandlerCallback) OnRemoteAudioTrack(sampleRate uint32, channels uint16) (pion.PionTrackCallback, error) {
	//TODO implement me
	panic("implement me")
}

func (handler *HandlerCallback) OnLocalAudioTrack(sampleRate uint32, channels uint16, callback pion.PionTrackCallback) error {
	//TODO implement me
	panic("implement me")
}

func (handler *HandlerCallback) OnError(err error) {
	//TODO implement me
	panic("implement me")
}
