import { Component, OnInit } from '@angular/core';
import { AiService } from '../ai.service';

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.scss']
})
export class UploadComponent  {

  constructor(private aiservice:AiService) { }

  selectedFile!: File;
  successMsg = '';
  errorMsg = '';

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
    this.successMsg='';
    this.errorMsg='';
  }

  upload() {
    if(!this.selectedFile) return;
    this.aiservice.upload(this.selectedFile).subscribe({
        next: (res) => {
          this.successMsg='Document uploaded & processed successfully!';
          this.errorMsg = '';
        },
        error: (err) => {
          this.errorMsg = 'Upload failed. Please try again.';
          this.successMsg = '';
          console.error(err);
        }
    })
  }

}
