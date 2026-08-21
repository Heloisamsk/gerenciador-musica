import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AlbumRequest } from '../models/AlbumRequestModel';
import { AlbumResponse } from '../models/AlbumResponse';

@Injectable({
  providedIn: 'root'
})
export class AdminAlbumService {

  private readonly apiUrl =
    `${environment.apiUrl}/api/admin/albuns`;

  constructor(
    private readonly http: HttpClient
  ) {}

  cadastrarAlbum(
    album: AlbumRequest
  ): Observable<AlbumResponse> {
    return this.http.post<AlbumResponse>(
      this.apiUrl,
      album
    );
  }
}
