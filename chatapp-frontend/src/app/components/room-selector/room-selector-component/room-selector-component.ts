import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule} from '@angular/common';
@Component({

  selector: 'app-room-selector-component',
  imports: [CommonModule],
  templateUrl: './room-selector-component.html',
  styleUrl: './room-selector-component.scss',
})
export class RoomSelectorComponent {

  rooms: string[] = ['general', 'random', 'devops'];

  @Output() roomSelected = new EventEmitter<string>();

  selectRoom(roomId: string):void{
    this.roomSelected.emit(roomId);
  }

}
