package pion

import "github.com/pion/webrtc/v4"

type IceServerOpt struct {
	url  string
	user *string
	auth *string
}

func (opt IceServerOpt) toRtc() webrtc.ICEServer {
	ret := webrtc.ICEServer{
		URLs: []string{opt.url},
	}
	if opt.user != nil {
		ret.Username = *opt.user
	}
	if opt.auth != nil {
		ret.Credential = *opt.auth
		ret.CredentialType = webrtc.ICECredentialTypePassword
	}
	return ret
}

type ConstructOpts struct {
	Ice          []IceServerOpt
	BundlePolicy webrtc.BundlePolicy
	Callback     PionPeerCallback
	Id           string
}

func (opt ConstructOpts) toRtc() webrtc.Configuration {
	ice := make([]webrtc.ICEServer, len(opt.Ice))
	for i, serverOpt := range opt.Ice {
		ice[i] = serverOpt.toRtc()
	}
	return webrtc.Configuration{
		ICEServers:   ice,
		BundlePolicy: opt.BundlePolicy,
	}
}

func ConstructPeer(opts ConstructOpts) (*PionPeer, error) {
	conn, err := webrtc.NewPeerConnection(opts.toRtc())
	if err != nil {
		return nil, err
	}
	ret := PionPeer{
		PeerConnection: conn,
		callback:       opts.Callback,
	}
	if err = ret.init(opts.Id); err != nil {
		_ = ret.Close()
		return nil, err
	}
	return &ret, nil
}
