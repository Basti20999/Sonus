package main

import (
	"os"

	"minceraft.dev/sonus/webrtc-pion/ipc"
)

func main() {
	socketPath := os.Getenv("PION_IPC_SOCKET")
	if socketPath == "" {
		socketPath = "/tmp/pion.socket"
	}
	ipc.BindSocket(socketPath)
}
