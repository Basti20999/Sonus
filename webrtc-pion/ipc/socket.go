package ipc

type Socket struct {
	handlers map[uint32]*Handler
}

func (socket *Socket) Send(msg IpcMessage) error {

}
