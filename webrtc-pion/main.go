package main

import (
	"fmt"
	"os"

	"minceraft.dev/sonus/webrtc-pion/ipc/socket"
)

func main() {
	socketPath := os.Getenv("PION_IPC_SOCKET")
	if socketPath == "" {
		socketPath = "/tmp/pion.socket"
	}
	fmt.Printf("using socket path: %s\n", socketPath)

	err := socket.BindSocket(socketPath)
	if err != nil {
		panic(err)
	}
}
