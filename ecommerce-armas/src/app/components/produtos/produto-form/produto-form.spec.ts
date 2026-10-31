import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { ProdutoForm } from './produto-form';

describe('ProdutoForm', () => {
  it('carrega apenas modelos e fornecedores da marca escolhida e limpa a selecao ao trocar', async () => {
    await TestBed.configureTestingModule({
      imports: [ProdutoForm],
      providers: [
        provideHttpClient(), provideHttpClientTesting(), provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { data: {} } } },
      ],
    }).compileComponents();
    const component = TestBed.createComponent(ProdutoForm).componentInstance;
    const http = TestBed.inject(HttpTestingController);
    component.ngOnInit();
    http.expectOne('http://localhost:8080/categorias').flush([]);
    http.expectOne('http://localhost:8080/marcas').flush([
      { id: 1, nome: 'Marca A' }, { id: 2, nome: 'Marca B' },
    ]);

    component.formGroup.get('marcaId')?.setValue(1);
    http.expectOne('http://localhost:8080/modelos?marcaId=1').flush([
      { id: 10, nome: 'Modelo A', marcaId: 1, marcaNome: 'Marca A' },
    ]);
    http.expectOne('http://localhost:8080/fornecedores?marcaId=1').flush([
      { id: 20, nome: 'Fornecedor A', marcaId: 1, marcaNome: 'Marca A' },
    ]);
    component.formGroup.patchValue({ modeloId: 10, fornecedorId: 20 });
    expect(component.modelos.length).toBe(1);

    component.formGroup.get('marcaId')?.setValue(2);
    expect(component.formGroup.get('modeloId')?.value).toBeNull();
    expect(component.formGroup.get('fornecedorId')?.value).toBeNull();
    http.expectOne('http://localhost:8080/modelos?marcaId=2').flush([]);
    http.expectOne('http://localhost:8080/fornecedores?marcaId=2').flush([]);
    http.verify();
    component.ngOnDestroy();
  });
});
