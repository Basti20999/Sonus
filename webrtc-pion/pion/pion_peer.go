package pion

import (
	"fmt"
	"strings"
	"time"

	"github.com/pion/rtp/codecs"
	"github.com/pion/webrtc/v4"
	"github.com/pion/webrtc/v4/pkg/media"
	"github.com/pion/webrtc/v4/pkg/media/samplebuilder"
)

type PionTrackCallback func(data []byte, duration time.Duration)

type PionPeerCallback interface {
	OnIceCandidate(candidate string, sdpMid *string, sdpMLineIndex *uint16)
	OnIceConnectionStateChange(state int)
	OnConnectionStateChange(state int)
	onRemoteAudioTrack(sampleRate uint32, channels uint16) PionTrackCallback
	onLocalAudioTrack(sampleRate uint32, channels uint16, callback PionTrackCallback)
	onError(err error)
}

type PionPeer struct {
	*webrtc.PeerConnection
	callback    PionPeerCallback
	activeTrack *string
	closed      bool
}

func (peer *PionPeer) init(id string) error {
	// we want to receive an audio track
	if _, err := peer.AddTransceiverFromKind(webrtc.RTPCodecTypeAudio, webrtc.RTPTransceiverInit{
		Direction: webrtc.RTPTransceiverDirectionRecvonly,
	}); err != nil {
		return err
	}

	// and we want to send a local track
	outputTrack, err := webrtc.NewTrackLocalStaticSample(webrtc.RTPCodecCapability{
		MimeType:     txCodec,
		ClockRate:    txSampleRate,
		Channels:     txChannels,
		SDPFmtpLine:  "",
		RTCPFeedback: nil,
	}, "tx"+id, "tx"+id)
	if err != nil {
		return err
	} else if err = peer.initOutputTrack(outputTrack); err != nil {
		return err
	}

	// handle events and stuff
	peer.OnICECandidate(peer.handleLocalCandidate)
	peer.OnICEConnectionStateChange(peer.handleIceConnectionStateChange)
	peer.OnConnectionStateChange(peer.handleConnectionStateChange)

	// listen for incoming audio tracks
	peer.OnTrack(func(track *webrtc.TrackRemote, receiver *webrtc.RTPReceiver) {
		codec := track.Codec()
		if strings.EqualFold(codec.MimeType, webrtc.MimeTypeOpus) {
			go peer.handleOpusTrack(track)
		} else {
			peer.callback.onError(fmt.Errorf("expected incoming opus track, received %s", codec.MimeType))
		}
	})

	return nil
}

func (peer *PionPeer) handleLocalCandidate(candidate *webrtc.ICECandidate) {
	iceCandidate, err := candidate.ToICE()
	if err != nil {
		peer.callback.onError(err)
		return
	}
	// pass to callback
	var sdpMid *string
	if candidate.SDPMid != "" {
		sdpMid = &candidate.SDPMid
	}
	var sdpMLineIndex *uint16
	if candidate.SDPMLineIndex != 0 {
		sdpMLineIndex = &candidate.SDPMLineIndex
	}
	peer.callback.OnIceCandidate(iceCandidate.Marshal(), sdpMid, sdpMLineIndex)
}

func (peer *PionPeer) handleIceConnectionStateChange(state webrtc.ICEConnectionState) {
	peer.callback.OnIceConnectionStateChange(int(state))
}

func (peer *PionPeer) handleConnectionStateChange(state webrtc.PeerConnectionState) {
	peer.callback.OnConnectionStateChange(int(state))
}

func (peer *PionPeer) initOutputTrack(track *webrtc.TrackLocalStaticSample) error {
	sender, err := peer.AddTrack(track)
	if err != nil {
		return err
	}

	// read incoming RTCP packets
	go func() {
		rtcpBuf := make([]byte, 1500)
		for !peer.closed {
			if _, _, rtcpErr := sender.Read(rtcpBuf); rtcpErr != nil {
				peer.callback.onError(rtcpErr)
				return
			}
		}
	}()

	// push callback to api consumer and let them write samples
	codec := track.Codec()
	peer.callback.onLocalAudioTrack(codec.ClockRate, codec.Channels, func(data []byte, duration time.Duration) {
		writeError := track.WriteSample(media.Sample{Data: data, Duration: duration})
		if writeError != nil {
			peer.callback.onError(writeError)
		}
	})
	return nil
}

func (peer *PionPeer) handleOpusTrack(track *webrtc.TrackRemote) {
	codec := track.Codec()
	callback := peer.callback.onRemoteAudioTrack(codec.ClockRate, codec.Channels)
	if callback == nil {
		return // track got ignored
	}
	// set as active track
	trackId := track.ID()
	peer.activeTrack = &trackId

	// continuously handle samples
	sb := samplebuilder.New(100, &codecs.OpusPacket{}, codec.ClockRate)
	for peer.isTrackActive(track) {
		packet, _, err := track.ReadRTP()
		if !peer.isTrackActive(track) {
			break
		} else if err != nil {
			peer.callback.onError(err)
			peer.activeTrack = nil
			break
		}
		// de-packetize the packet
		sb.Push(packet)
		// handle samples
		var sample *media.Sample
		for {
			sample = sb.Pop()
			if sample == nil {
				break // no more samples
			}
			// push sample to callback
			callback(sample.Data, sample.Duration)
		}
	}
}

func (peer *PionPeer) isTrackActive(track *webrtc.TrackRemote) bool {
	return !peer.closed &&
		track != nil &&
		peer.activeTrack != nil &&
		*peer.activeTrack == track.ID()
}

func (peer *PionPeer) HandleOffer(sdp string) (string, error) {
	if err := peer.SetRemoteDescription(webrtc.SessionDescription{Type: webrtc.SDPTypeOffer, SDP: sdp}); err != nil {
		return "", err
	}
	answer, err := peer.CreateAnswer(nil)
	if err != nil {
		return "", err
	} else if err = peer.SetLocalDescription(answer); err != nil {
		return "", err
	}
	return answer.SDP, nil
}

func (peer *PionPeer) AddIceCandidate(candidate string, sdpMid *string, sdpMLineIndex *uint16) error {
	return peer.AddICECandidate(webrtc.ICECandidateInit{
		Candidate:     candidate,
		SDPMid:        sdpMid,
		SDPMLineIndex: sdpMLineIndex,
	})
}

func (peer *PionPeer) Close() error {
	peer.closed = true
	return peer.PeerConnection.Close()
}

func (peer *PionPeer) String() string {
	return fmt.Sprintf("Peer[%s]", peer.ID())
}
