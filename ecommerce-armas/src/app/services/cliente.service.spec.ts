import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ClienteService } from './cliente.service';

describe('ClienteService', () => {
  let service: ClienteService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ClienteService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should list clients from the API', () => {
    service.listar().subscribe((clientes) => expect(clientes.length).toBe(1));

    const request = http.expectOne('http://localhost:8080/clientes');
    expect(request.request.method).toBe('GET');
    request.flush([{ id: 1, nome: 'Cliente', email: 'cliente@teste.com', telefone: '123', endereco: 'Rua' }]);
  });
});
