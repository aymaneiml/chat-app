export enum MessageType {
    CHAT = 'CHAT',
    JOIN = 'JOIN',
    LEAVE = 'LEAVE'
}

export interface ChatMessage{
    roomId: string;
    sender: string;
    content: string | null;
    type: MessageType;
    timestamp: string;
}

export interface ChatMessageRequest{
    sender:string;
    content:string;
}