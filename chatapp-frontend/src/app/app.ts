import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { RoomSelectorComponent } from './components/room-selector/room-selector-component/room-selector-component';
import { ChatRoomComponent } from './components/chat-room/chat-room-component/chat-room-component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet,RoomSelectorComponent,ChatRoomComponent,CommonModule],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  selectedRoomId: string | null = null;
  username = 'aymane'; // en dur pour l'instant, à remplacer par un vrai formulaire plus tard

  onRoomSelected(roomId: string): void {
    this.selectedRoomId = roomId;
  }

}
