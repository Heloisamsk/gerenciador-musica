import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MusicaResponse } from '../models/MusicaResponse';

@Injectable({
  providedIn: 'root'
})
export class AdminMusicaService {
  
  private apiUrl = 'http://localhost:8080/api/musicas';

  constructor(private http: HttpClient) {}

  listarMusicas(): Observable<MusicaResponse[]> {
    return this.http.get<MusicaResponse[]>(this.apiUrl);
  }
}