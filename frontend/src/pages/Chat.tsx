import { Client } from '@stomp/stompjs'
import { RefObject, useEffect, useRef, useState } from 'react'
import { decrypt, encrypt } from '../util/encryption'

export default () => {
    const [recipient, setRecipient] = useState('')
    const [text, setText] = useState('')
    const [message, setMessage] = useState('')
    const stompClientRef: RefObject<Client | null> = useRef(null)

    useEffect(() => {
        const wsPath = 'ws://localhost:8080/ws'
        const client = new Client({
            brokerURL: wsPath,
            onConnect: () => {
                console.log('connected')
                client.subscribe('/user/queue/messages', msg => {
                    const message = JSON.parse(decrypt(msg.body))
                    console.log(`${message.from}: ${message.content}`)
                    setMessage(message.content)
                })
            },
            onStompError: frame => {
                console.error('broker reported error: ' + frame.headers['message'])
                console.error('additional details: ' + frame.body)
            },
            webSocketFactory: () => new WebSocket(wsPath)
        })
        
        client.activate()
        stompClientRef.current = client

        return () => {
            client.deactivate()
        }
    }, [])

    const sendMessage = () => {
        const client = stompClientRef.current
        if (client && client.connected) {
            console.log(`sending message: ${text}`)
            client.publish({
                destination: '/app/send',
                body: encrypt(JSON.stringify({
                    to: recipient,
                    content: text
                }))
            })
        } else {
            console.error('stomp client is not connected')
        }
    }

    return (
        <div>
            <p>{message}</p>
            <input type='text' value={recipient} onChange={e => setRecipient(e.target.value)} />
            <input type='text' value={text} onChange={e => setText(e.target.value)} />
            <button onClick={sendMessage}>Send</button>
        </div>
    )
}