import { Component, OnInit } from '@angular/core';
import { AiService } from '../ai.service';

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.scss']
})
export class UploadComponent  {

  constructor(private aiservice:AiService) { }

  uploadedDocs: any[] = [];
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
    const file = this.selectedFile;
    // Add to local UI list immediately
    this.uploadedDocs.push({
      name: file.name
    });

    const currentIndex = this.uploadedDocs.length - 1;

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
  removeDoc(index: number) {
    this.uploadedDocs.splice(index, 1);
  }
}
