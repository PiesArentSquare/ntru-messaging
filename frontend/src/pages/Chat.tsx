import { Client } from '@stomp/stompjs'
import { createRef, FormEvent, RefObject, useEffect, useRef, useState } from 'react'
import { NTRU } from '../util/ntru'
import Message from '../components/Message'

interface Msg {
    mine: boolean
    body: string
}

export default () => {
    const [recipient, setRecipient] = useState('')
    const [text, setText] = useState('')
    const [messages, setMessages] = useState<Msg[]>([])
    const messageBlockRef = createRef<HTMLDivElement>()
    const inputRef = createRef<HTMLInputElement>()

    const stompClientRef: RefObject<Client | null> = useRef(null)
    const ntruRef: RefObject<NTRU | null> = useRef(null)

    useEffect(() => {
        const ntru = new NTRU()
        const wsPath = `ws://${window.location.host}/api/ws`
        const client = new Client({
            brokerURL: wsPath,
            onConnect: () => {
                console.log('connected')
                client.subscribe('/user/queue/publicKey', msg => {
                    ntru.setForeignKey(msg.body)
                })
                client.publish({
                    destination: '/app/publicKey',
                    body: JSON.stringify({key: ntru.getPublicKey()}),
                })
                client.subscribe('/user/queue/messages', msg => {
                    const message = JSON.parse(ntru!.decrypt(msg.body))
                    console.log(`${message.from}: ${message.content}`)
                    addMessage({ mine: false, body: message.content })
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
        ntruRef.current = ntru

        return () => {
            client.deactivate()
        }
    }, [])

    useEffect(() => {
        messageBlockRef.current?.lastElementChild?.scrollIntoView({behavior: 'smooth'})
    }, [messages])

    const sendMessage = (e: FormEvent) => {
        e.preventDefault()
        if (!text)
            return
        const client = stompClientRef.current
        const ntru = ntruRef.current
        console.log(ntru, client, client?.connected)
        if (client && client.connected && ntru) {
            console.log(`sending message: ${text}`)
            client.publish({
                destination: '/app/send',
                body: ntru.encrypt(JSON.stringify({
                    to: recipient,
                    content: text
                }))
            })
            addMessage({ mine: true, body: text })
            setText('')
        } else {
            console.error('stomp client is not connected')
        }
    }

    const addMessage = (msg: Msg) => {
        setMessages(m => [...m, msg])
    }

    return (
        <div className='h-screen flex flex-col bg-brand-background-10 text-brand-text-900'>
            <nav className='p-4 flex-grow-0 flex-shrink flex justify-center'>
                <input type='text' placeholder='recipient' className='text-center text-4xl font-bold placeholder:text-brand-text-900/50' value={recipient} onChange={e => setRecipient(e.target.value)} />
            </nav>
            <div className='flex m-auto justify-center flex-shrink overflow-auto relative mask-t-from-90% w-[100ch] max-w-full'>
                <div className='h-full w-full flex flex-col overflow-y-scroll gap-4 before:content-[""] before:h-20 before:w-full before:flex-shrink-0' ref={messageBlockRef}>
                    {messages.map((msg, i) => <Message key={i} mine={msg.mine} body={msg.body} />)}
                </div>
            </div>
            <div className='flex-grow-0 flex-shrink self-center flex gap-4 w-[100ch] max-w-full'>
                <form className='flex flex-shrink w-[100ch] p-4' onSubmit={sendMessage}>
                    <input type='text' placeholder='Type something...' ref={inputRef} className='flex-grow flex-shrink px-4 py-1 mx-4 border-b-2 border-b-brand-text-900/50 placeholder:text-brand-text-900/50 outline-none hover:border-b-brand-primary-500 hover:placeholder:text-brand-primary-500 duration-200 transition-colors' value={text} onChange={e => setText(e.target.value)}/>
                    <button type='submit' className='py-1 px-4 bg-brand-accent-300 rounded-full hover:bg-brand-accent-400 duration-100 hover:cursor-pointer'>Send</button>
                </form>
            </div>
        </div>
    )
}