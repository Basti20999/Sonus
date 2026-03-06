package ipc

type Socket struct {
	Handlers map[uint32]*Handler
}

func (socket *Socket) Send(msg IpcMessage) error {

}
