package main

import (
	"log"
	"os"

	"minceraft.dev/sonus/webrtc-pion/ipc/socket"
)

func main() {
	socketPath := os.Getenv("PION_IPC_SOCKET")
	if socketPath == "" {
		if len(os.Args) > 1 {
			socketPath = os.Args[1]
		} else {
			// default path
			socketPath = "/tmp/pion.socket"
		}
	}
	log.Printf("using socket path: %s", socketPath)

	err := socket.BindSocket(socketPath)
	if err != nil {
		panic(err)
	}
}
