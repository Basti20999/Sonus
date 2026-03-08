package pion

import (
	"github.com/pion/webrtc/v4"
)

const (
	codec      = webrtc.MimeTypeOpus
	sampleRate = 48_000
	rxChannels = 1
	txChannels = 2
)
