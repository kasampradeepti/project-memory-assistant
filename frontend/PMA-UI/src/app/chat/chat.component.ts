import { Component, OnInit } from '@angular/core';
import { AiService } from '../ai.service';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss']
})
export class ChatComponent implements OnInit {

  constructor(private aiservice:AiService) { }

  ngOnInit(): void {
  }

    response:string ='';
    question:string='';

    // send(){
    //   this.aiservice.ask(this.question).subscribe(
    //     res=> this.response=res,
    //     err=> this.response="Error: "+err.message
    //   );
    //   this.question='';
    // }

    messages: any[] = [];

send() {
  if (!this.question.trim()) return;

  // Push user message
  this.messages.push({ sender: 'user', text: this.question });

  const q = this.question;
  this.question = '';  // clear input

  this.aiservice.ask(q).subscribe(res => {
    console.log(res);
    this.messages.push({ sender: 'ai', text: res });
  });
}
    
}
