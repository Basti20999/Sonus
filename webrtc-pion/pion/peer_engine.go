package pion

import (
	"github.com/pion/interceptor"
	"github.com/pion/webrtc/v4"
)

func constructApi() *webrtc.API {
	mediaEngine := webrtc.MediaEngine{}

	// register rx codec
	if err := mediaEngine.RegisterCodec(webrtc.RTPCodecParameters{
		RTPCodecCapability: webrtc.RTPCodecCapability{
			MimeType: codec, ClockRate: sampleRate, Channels: rxChannels, SDPFmtpLine: "", RTCPFeedback: nil,
		},
		PayloadType: 42,
	}, webrtc.RTPCodecTypeAudio); err != nil {
		panic(err)
	}
	// register tx codec
	if err := mediaEngine.RegisterCodec(webrtc.RTPCodecParameters{
		RTPCodecCapability: webrtc.RTPCodecCapability{
			MimeType: codec, ClockRate: sampleRate, Channels: txChannels, SDPFmtpLine: "", RTCPFeedback: nil,
		},
		PayloadType: 84,
	}, webrtc.RTPCodecTypeAudio); err != nil {
		panic(err)
	}

	// some random defaults, Idk
	interceptorRegistry := interceptor.Registry{}
	if err := webrtc.RegisterDefaultInterceptors(&mediaEngine, &interceptorRegistry); err != nil {
		panic(err)
	}

	return webrtc.NewAPI(
		webrtc.WithMediaEngine(&mediaEngine),
		webrtc.WithInterceptorRegistry(&interceptorRegistry),
	)
}

var WebrtcApi = constructApi()
