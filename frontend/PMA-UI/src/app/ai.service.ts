import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AiService {

  private apiUrl = 'http://localhost:8080/api/v1/ask';
  private docUploadUrl = 'http://localhost:8080/api/v1/docs/upload';

  constructor(private http: HttpClient) { }

  ask(question: string): Observable<string>{
      return this.http.get(this.apiUrl,{
        responseType: 'text',
        params: {q: question }
      });
  }

  upload(file:File):Observable<string> {
    const formData = new FormData;
    formData.append('file',file);
    return this.http.post(this.docUploadUrl,formData,{
      responseType: 'text'
    });
  }
}
