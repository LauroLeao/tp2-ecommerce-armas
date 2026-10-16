import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ProdutoService } from './produto.service';

describe('ProdutoService', () => {
  let service: ProdutoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ProdutoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('should request a filtered page from the API', () => {
    service.listar('Fuzil', 1, 5).subscribe((pagina) => {
      expect(pagina.items.length).toBe(1);
      expect(pagina.totalItems).toBe(6);
    });

    const request = http.expectOne((req) => req.url === 'http://localhost:8080/produtos');
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('nome')).toBe('Fuzil');
    expect(request.request.params.get('page')).toBe('1');
    expect(request.request.params.get('pageSize')).toBe('5');
    request.flush({
      items: [{ id: 1, nome: 'Fuzil', descricao: '', preco: 10, estoque: 1, categoriaId: 2, categoriaNome: 'Fuzil' }],
      page: 1,
      pageSize: 5,
      totalItems: 6
    });
  });
});
