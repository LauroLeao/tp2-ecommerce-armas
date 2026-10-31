import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { ClienteForm } from './cliente-form';

describe('ClienteForm', () => {
  let component: ClienteForm;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClienteForm],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { data: {} } } },
      ],
    }).compileComponents();
    component = TestBed.createComponent(ClienteForm).componentInstance;
    component.ngOnInit();
    http = TestBed.inject(HttpTestingController);
    http.expectOne('https://servicodados.ibge.gov.br/api/v1/localidades/estados?orderBy=nome')
      .flush([{ id: 35, sigla: 'SP', nome: 'São Paulo' }]);
  });

  afterEach(() => http.verify());

  it('consulta somente CEP completo e preenche municipio e estado', () => {
    component.formGroup.get('cep')?.setValue('01001');
    component.consultarCep();
    http.expectNone('https://viacep.com.br/ws/01001/json/');

    component.formGroup.get('cep')?.setValue('01001000');
    component.consultarCep();
    http.expectOne('https://viacep.com.br/ws/01001000/json/').flush({
      cep: '01001-000', logradouro: 'Praça da Sé', complemento: '',
      bairro: 'Sé', localidade: 'São Paulo', uf: 'SP',
      estado: 'São Paulo', ibge: '3550308',
    });
    http.expectOne('https://servicodados.ibge.gov.br/api/v1/localidades/estados/SP/municipios?orderBy=nome')
      .flush([{ id: 3550308, nome: 'São Paulo' }]);

    expect(component.formGroup.get('municipioCodigoIbge')?.value).toBe('3550308');
    expect(component.formGroup.get('estadoSigla')?.value).toBe('SP');
  });

  it('mostra erro quando o CEP nao existe e permite tentar novamente', () => {
    component.formGroup.get('cep')?.setValue('99999999');
    component.consultarCep();
    http.expectOne('https://viacep.com.br/ws/99999999/json/').flush({ erro: true });
    expect(component.erroCep).toContain('não encontrado');

    component.consultarCep();
    http.expectOne('https://viacep.com.br/ws/99999999/json/').flush({
      cep: '99999-999', logradouro: 'Rua Teste', complemento: '',
      bairro: 'Centro', localidade: 'Cidade', uf: 'SP',
      estado: 'São Paulo', ibge: '3550308',
    });
    http.expectOne('https://servicodados.ibge.gov.br/api/v1/localidades/estados/SP/municipios?orderBy=nome')
      .flush([{ id: 3550308, nome: 'São Paulo' }]);
    expect(component.erroCep).toBe('');
  });

  it('associa ao campo o erro de duplicidade retornado pelo back-end', () => {
    component.formGroup.patchValue({
      nome: 'Cliente de teste', email: 'cliente@teste.com', telefone: '11999999999',
      cpf: '12345678909', cep: '01001000', numero: '100',
    });
    component.consultarCep();
    http.expectOne('https://viacep.com.br/ws/01001000/json/').flush({
      cep: '01001-000', logradouro: 'Praça da Sé', complemento: '',
      bairro: 'Sé', localidade: 'São Paulo', uf: 'SP',
      estado: 'São Paulo', ibge: '3550308',
    });
    http.expectOne('https://servicodados.ibge.gov.br/api/v1/localidades/estados/SP/municipios?orderBy=nome')
      .flush([{ id: 3550308, nome: 'São Paulo' }]);
    expect(component.formGroup.valid).toBe(true);

    component.salvar();
    const request = http.expectOne('http://localhost:8080/clientes');
    expect(request.request.body.municipioCodigoIbge).toBe('3550308');
    request.flush(
      { field: 'email', message: 'Este e-mail ja esta cadastrado.' },
      { status: 409, statusText: 'Conflict' },
    );
    expect(component.formGroup.get('email')?.getError('api')).toContain('cadastrado');
  });

  it('permite cadastrar endereço sem CEP com estado e município selecionados', () => {
    const estado = { id: 35, sigla: 'SP', nome: 'São Paulo' };
    const municipio = { id: 3550308, nome: 'São Paulo' };
    component.formGroup.patchValue({
      nome: 'Cliente manual', email: 'manual@teste.com', telefone: '11999999999',
      cpf: '12345678909', cep: '', logradouro: 'Rua das Flores', numero: '100', bairro: 'Centro',
    });
    component.estadoBusca.setValue(estado);
    component.selecionarEstado(estado);
    http.expectOne('https://servicodados.ibge.gov.br/api/v1/localidades/estados/SP/municipios?orderBy=nome')
      .flush([municipio]);
    component.municipioBusca.setValue(municipio);
    component.selecionarMunicipio(municipio);

    expect(component.formGroup.valid).toBe(true);
    expect(component.estadoBusca.valid).toBe(true);
    expect(component.municipioBusca.valid).toBe(true);
    component.salvar();
    const request = http.expectOne('http://localhost:8080/clientes');
    expect(request.request.body.cep).toBeNull();
    expect(request.request.body.municipioCodigoIbge).toBe('3550308');
    expect(request.request.body.estadoSigla).toBe('SP');
    request.flush({ field: 'email', message: 'Teste encerrado.' }, { status: 409, statusText: 'Conflict' });
  });

  it('rejeita arquivo que não seja JPG ou PNG sem alterar a foto atual', async () => {
    component.fotoPreview.set('http://localhost:8080/clientes/1/foto');
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', { value: [new File(['texto'], 'foto.txt', { type: 'text/plain' })] });
    await component.selecionarFoto({ target: input } as unknown as Event);
    expect(component.erroFoto()).toContain('JPG ou PNG');
    expect(component.fotoPreview()).toContain('/foto');
  });

  it('envia pedido de remoção da foto ao salvar', () => {
    component.fotoPreview.set('http://localhost:8080/clientes/1/foto');
    component.retirarFoto();
    const estado = { id: 35, sigla: 'SP', nome: 'São Paulo' };
    const municipio = { id: 3550308, nome: 'São Paulo' };
    component.formGroup.patchValue({
      nome: 'Cliente teste', email: 'foto@teste.com', telefone: '11999999999',
      cpf: '12345678909', logradouro: 'Rua das Flores', numero: '100', bairro: 'Centro',
    });
    component.estadoBusca.setValue(estado);
    component.selecionarEstado(estado);
    http.expectOne('https://servicodados.ibge.gov.br/api/v1/localidades/estados/SP/municipios?orderBy=nome')
      .flush([municipio]);
    component.municipioBusca.setValue(municipio);
    component.selecionarMunicipio(municipio);
    component.salvar();
    const request = http.expectOne('http://localhost:8080/clientes');
    expect(request.request.body.removerFoto).toBe(true);
    expect(request.request.body.fotoBase64).toBeUndefined();
    request.flush({ mensagem: 'Teste encerrado.' }, { status: 400, statusText: 'Bad Request' });
  });
});
