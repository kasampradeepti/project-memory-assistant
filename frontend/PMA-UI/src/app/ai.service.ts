import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AiService {

  private apiUrl = 'http://localhost:8080/api/v1/ask';

  constructor(private http: HttpClient) { }

  ask(question: string): Observable<string>{
      return this.http.get(this.apiUrl,{
        responseType: 'text',
        params: {q: question }
      });
  }
}
