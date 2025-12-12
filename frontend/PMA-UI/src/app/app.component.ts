import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'PMA-UI';
  showUpload = false;

toggleUploadPanel() {
  this.showUpload = !this.showUpload;
}

}
