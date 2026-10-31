import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { CadastroAdmin } from './cadastro-admin';

describe('CadastroAdmin', () => {
  it('cadastra e edita uma marca na mesma tela', async () => {
    await TestBed.configureTestingModule({
      imports: [CadastroAdmin],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { data: { tipo: 'marcas' } } } }],
    }).compileComponents();
    const component = TestBed.createComponent(CadastroAdmin).componentInstance;
    const http = TestBed.inject(HttpTestingController);
    component.ngOnInit();
    http.expectOne('http://localhost:8080/marcas').flush([]);

    component.formGroup.patchValue({ nome: 'Marca A' });
    component.salvar();
    const cadastro = http.expectOne('http://localhost:8080/marcas');
    expect(cadastro.request.method).toBe('POST');
    cadastro.flush({ id: 1, nome: 'Marca A' });
    http.expectOne('http://localhost:8080/marcas').flush([{ id: 1, nome: 'Marca A' }]);

    component.selecionar(component.registros[0]);
    component.formGroup.patchValue({ nome: 'Marca B' });
    component.salvar();
    const edicao = http.expectOne('http://localhost:8080/marcas/1');
    expect(edicao.request.method).toBe('PUT');
    expect(edicao.request.body.nome).toBe('Marca B');
    edicao.flush({ id: 1, nome: 'Marca B' });
    http.expectOne('http://localhost:8080/marcas').flush([{ id: 1, nome: 'Marca B' }]);
    http.match('http://localhost:8080/modelos?marcaId=1').forEach((pedido) => pedido.flush([]));
    expect(component.registros[0].nome).toBe('Marca B');
    http.verify();
  });
});
