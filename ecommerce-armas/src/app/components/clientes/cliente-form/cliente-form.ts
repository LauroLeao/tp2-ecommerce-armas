import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { ClienteService } from '../../../services/cliente.service';
import { Subscription } from 'rxjs';
import { Cliente, ClienteRequest } from '../../../models/cliente.model';
import { CepService } from '../../../services/cep.service';
import { cpfValidator } from '../../../models/cpf.validator';
import { EstadoIbge, MunicipioIbge } from '../../../models/localidade.model';
import { LocalidadeService } from '../../../services/localidade.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatAutocompleteModule,
  ],
  selector: 'app-cliente-form',
  styleUrl: './cliente-form.css',
  templateUrl: './cliente-form.html',
})
export class ClienteForm implements OnInit, OnDestroy {
  private readonly activatedRoute = inject(ActivatedRoute);
  readonly modoPublico = this.activatedRoute.snapshot.routeConfig?.path === 'cadastro';
  readonly modoPerfil = this.activatedRoute.snapshot.routeConfig?.path === 'perfil';
  formGroup!: FormGroup;
  salvando = false;
  excluindo = false;
  erro = '';
  erroCep = '';
  erroLocalidades = '';
  consultandoCep = false;
  carregandoEstados = false;
  carregandoMunicipios = false;
  enderecoAntigo = '';
  readonly fotoPreview = signal<string | null>(null);
  readonly nomeFoto = signal('');
  readonly erroFoto = signal('');
  readonly carregandoFoto = signal(false);
  private fotoBase64: string | null = null;
  private removerFoto = false;
  private fotoOperacao = 0;
  private readonly tamanhoMaximoFoto = 5 * 1024 * 1024;
  readonly estadoBusca = new FormControl<EstadoIbge | string>('', {
    nonNullable: true,
    validators: [(control) => typeof control.value === 'object' && control.value !== null ? null : { selecao: true }],
  });
  readonly municipioBusca = new FormControl<MunicipioIbge | string>('', {
    nonNullable: true,
    validators: [(control) => typeof control.value === 'object' && control.value !== null ? null : { selecao: true }],
  });
  estados: EstadoIbge[] = [];
  estadosFiltrados: EstadoIbge[] = [];
  municipios: MunicipioIbge[] = [];
  municipiosFiltrados: MunicipioIbge[] = [];
  private consultaCep?: Subscription;
  private alteracaoCep?: Subscription;
  private alteracaoEstado?: Subscription;
  private alteracaoMunicipio?: Subscription;
  private consultaMunicipios?: Subscription;
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly auth = inject(AuthService);

  get editando(): boolean {
    return Boolean(this.formGroup?.get('id')?.value);
  }

  get iniciais(): string {
    const nome = String(this.formGroup?.get('nome')?.value ?? '');
    const partes = nome.trim().split(/\s+/).filter(Boolean);
    return partes.length
      ? `${partes[0][0]}${partes.length > 1 ? partes[partes.length - 1][0] : ''}`.toUpperCase()
      : '?';
  }

  get enderecoResumo(): string {
    const logradouro = this.formGroup?.get('logradouro')?.value;
    const numero = this.formGroup?.get('numero')?.value;
    const municipio = this.formGroup?.get('municipioNome')?.value;
    const uf = this.formGroup?.get('estadoSigla')?.value;
    return logradouro && numero && municipio && uf
      ? `${logradouro}, ${numero} - ${municipio}/${uf}`
      : this.enderecoAntigo || 'Não informado';
  }

  constructor(
    private formBuilder: FormBuilder,
    private clienteService: ClienteService,
    private cepService: CepService,
    private localidadeService: LocalidadeService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    const cliente: Cliente = this.activatedRoute.snapshot.data['cliente'];
    this.enderecoAntigo = cliente?.municipioCodigoIbge ? '' : (cliente?.endereco ?? '');
    this.fotoPreview.set(this.modoPerfil && cliente?.fotoUrl
      ? `http://localhost:8080/auth/me/foto${cliente.fotoUrl.includes('?') ? '?' + cliente.fotoUrl.split('?')[1] : ''}`
      : this.clienteService.obterUrlFoto(cliente?.fotoUrl));
    this.nomeFoto.set(cliente?.fotoUrl ? 'Foto atual do cliente' : 'Nenhuma foto selecionada');

    this.formGroup = this.formBuilder.group({
      id: [cliente?.id ?? null],
      nome: [cliente?.nome ?? '', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(120)]],
      email: [cliente?.email ?? '', [Validators.required, Validators.email]],
      telefone: [cliente?.telefone ?? '', [Validators.required, Validators.pattern(/^\d{10,11}$/)]],
      cpf: [cliente?.cpf ?? '', [Validators.required, cpfValidator]],
      cep: [cliente?.cep ?? '', Validators.pattern(/^(?:\d{8})?$/)],
      logradouro: [cliente?.logradouro ?? '', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(120)]],
      numero: [cliente?.numero ?? '', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(20)]],
      complemento: [cliente?.complemento ?? '', Validators.maxLength(120)],
      bairro: [cliente?.bairro ?? '', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]],
      municipioNome: [cliente?.municipioNome ?? '', Validators.required],
      municipioCodigoIbge: [cliente?.municipioCodigoIbge ?? '', Validators.required],
      estadoSigla: [cliente?.estadoSigla ?? '', Validators.required],
      estadoNome: [cliente?.estadoNome ?? '', Validators.required],
      ...(this.modoPublico ? {
        senha: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
        confirmarSenha: ['', Validators.required],
      } : {}),
    });
    if (this.modoPerfil) this.formGroup.get('email')?.disable({ emitEvent: false });

    if (cliente?.estadoSigla && cliente.estadoNome) {
      this.estadoBusca.setValue({ id: 0, sigla: cliente.estadoSigla, nome: cliente.estadoNome }, { emitEvent: false });
    }
    if (cliente?.municipioCodigoIbge && cliente.municipioNome) {
      this.municipioBusca.setValue({ id: Number(cliente.municipioCodigoIbge), nome: cliente.municipioNome }, { emitEvent: false });
    }
    this.municipioBusca.disable({ emitEvent: false });
    this.carregarEstados();
    if (cliente?.estadoSigla) this.carregarMunicipios(cliente.estadoSigla);

    this.alteracaoCep = this.formGroup.get('cep')?.valueChanges.subscribe(() => {
      this.consultaCep?.unsubscribe();
      this.consultandoCep = false;
      this.erroCep = '';
    });
    this.alteracaoEstado = this.estadoBusca.valueChanges.subscribe((valor) => {
      this.estadosFiltrados = this.filtrarEstados(typeof valor === 'string' ? valor : valor.nome);
      if (typeof valor !== 'string') return;
      this.consultaCep?.unsubscribe();
      this.consultandoCep = false;
      this.erroCep = '';
      this.consultaMunicipios?.unsubscribe();
      this.carregandoMunicipios = false;
      this.municipios = [];
      this.municipiosFiltrados = [];
      this.municipioBusca.setValue('', { emitEvent: false });
      this.municipioBusca.disable({ emitEvent: false });
      this.formGroup.patchValue({ estadoSigla: '', estadoNome: '', municipioNome: '', municipioCodigoIbge: '' });
    });
    this.alteracaoMunicipio = this.municipioBusca.valueChanges.subscribe((valor) => {
      this.municipiosFiltrados = this.filtrarMunicipios(typeof valor === 'string' ? valor : valor.nome);
      if (typeof valor === 'string') {
        this.formGroup.patchValue({ municipioNome: '', municipioCodigoIbge: '' });
      }
    });
  }

  ngOnDestroy(): void {
    this.fotoOperacao++;
    this.consultaCep?.unsubscribe();
    this.alteracaoCep?.unsubscribe();
    this.alteracaoEstado?.unsubscribe();
    this.alteracaoMunicipio?.unsubscribe();
    this.consultaMunicipios?.unsubscribe();
  }

  async selecionarFoto(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const arquivo = input.files?.[0];
    input.value = '';
    if (!arquivo || this.salvando || this.excluindo) return;
    const operacao = ++this.fotoOperacao;
    this.erroFoto.set('');
    if (!['image/jpeg', 'image/png'].includes(arquivo.type)) {
      this.erroFoto.set('Escolha uma foto JPG ou PNG.');
      return;
    }
    if (arquivo.size > this.tamanhoMaximoFoto) {
      this.erroFoto.set('A foto deve ter no máximo 5 MB.');
      return;
    }
    this.carregandoFoto.set(true);
    try {
      const dados = await new Promise<string>((resolve, reject) => {
        const leitor = new FileReader();
        leitor.onload = () => typeof leitor.result === 'string' ? resolve(leitor.result) : reject(new Error('arquivo'));
        leitor.onerror = () => reject(new Error('arquivo'));
        leitor.readAsDataURL(arquivo);
      });
      const imagem = new Image();
      imagem.src = dados;
      await imagem.decode();
      if (operacao !== this.fotoOperacao) return;
      if (imagem.naturalWidth > 10_000 || imagem.naturalHeight > 10_000 ||
          imagem.naturalWidth * imagem.naturalHeight > 25_000_000) {
        throw new Error('dimensoes');
      }
      this.fotoBase64 = dados;
      this.fotoPreview.set(dados);
      this.nomeFoto.set(arquivo.name);
      this.removerFoto = false;
    } catch (erro) {
      if (operacao !== this.fotoOperacao) return;
      this.erroFoto.set(erro instanceof Error && erro.message === 'dimensoes'
        ? 'A foto tem resolução muito alta. Use até 25 megapixels e 10.000 pixels por lado.'
        : 'Não foi possível abrir essa foto. Escolha outro arquivo JPG ou PNG.');
    } finally {
      if (operacao === this.fotoOperacao) this.carregandoFoto.set(false);
    }
  }

  retirarFoto(): void {
    if (this.salvando || this.excluindo) return;
    this.fotoOperacao++;
    this.fotoBase64 = null;
    this.fotoPreview.set(null);
    this.nomeFoto.set('Nenhuma foto selecionada');
    this.removerFoto = true;
    this.erroFoto.set('');
    this.carregandoFoto.set(false);
  }

  exibirEstado(valor: EstadoIbge | string | null): string {
    return typeof valor === 'string' ? valor : valor?.nome ?? '';
  }

  exibirMunicipio(valor: MunicipioIbge | string | null): string {
    return typeof valor === 'string' ? valor : valor?.nome ?? '';
  }

  selecionarEstado(estado: EstadoIbge): void {
    this.consultaCep?.unsubscribe();
    this.consultandoCep = false;
    this.erroCep = '';
    this.formGroup.patchValue({ estadoSigla: estado.sigla, estadoNome: estado.nome, municipioNome: '', municipioCodigoIbge: '' });
    this.municipioBusca.setValue('', { emitEvent: false });
    this.carregarMunicipios(estado.sigla);
  }

  selecionarMunicipio(municipio: MunicipioIbge): void {
    this.formGroup.patchValue({ municipioNome: municipio.nome, municipioCodigoIbge: String(municipio.id) });
  }

  private carregarEstados(): void {
    this.carregandoEstados = true;
    this.localidadeService.listarEstados().subscribe({
      next: (estados) => {
        this.estados = estados;
        this.estadosFiltrados = this.filtrarEstados(this.exibirEstado(this.estadoBusca.value));
        this.carregandoEstados = false;
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.carregandoEstados = false;
        this.erroLocalidades = 'Não foi possível carregar os estados. Verifique a conexão e tente novamente.';
        this.changeDetector.markForCheck();
      },
    });
  }

  private carregarMunicipios(uf: string): void {
    this.consultaMunicipios?.unsubscribe();
    this.municipios = [];
    this.municipiosFiltrados = [];
    this.municipioBusca.disable({ emitEvent: false });
    this.carregandoMunicipios = true;
    this.erroLocalidades = '';
    this.consultaMunicipios = this.localidadeService.listarMunicipios(uf).subscribe({
      next: (municipios) => {
        if (this.formGroup.get('estadoSigla')?.value !== uf) return;
        this.municipios = municipios;
        this.municipiosFiltrados = this.filtrarMunicipios(this.exibirMunicipio(this.municipioBusca.value));
        this.municipioBusca.enable({ emitEvent: false });
        this.carregandoMunicipios = false;
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.carregandoMunicipios = false;
        this.erroLocalidades = 'Não foi possível carregar os municípios deste estado. Tente novamente.';
        this.changeDetector.markForCheck();
      },
    });
  }

  private normalizar(texto: string): string {
    return texto.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('pt-BR').trim();
  }

  private filtrarEstados(termo: string): EstadoIbge[] {
    const busca = this.normalizar(termo);
    return this.estados.filter((estado) => this.normalizar(`${estado.nome} ${estado.sigla}`).includes(busca));
  }

  private filtrarMunicipios(termo: string): MunicipioIbge[] {
    const busca = this.normalizar(termo);
    if (busca.length < 2) return [];
    return this.municipios.filter((municipio) => this.normalizar(municipio.nome).includes(busca)).slice(0, 15);
  }

  somenteDigitos(campo: 'cpf' | 'telefone' | 'cep', limite: number): void {
    const control = this.formGroup.get(campo);
    const valor = String(control?.value ?? '');
    const digitos = valor.replace(/\D/g, '').slice(0, limite);
    if (valor !== digitos) control?.setValue(digitos);
  }

  consultarCep(): void {
    const control = this.formGroup.get('cep');
    const cep = String(control?.value ?? '');
    if (!/^\d{8}$/.test(cep)) {
      control?.markAsTouched();
      this.erroCep = 'Digite um CEP com 8 números para fazer a consulta.';
      return;
    }
    if (this.consultandoCep) return;

    this.consultandoCep = true;
    this.erroCep = '';
    this.consultaCep = this.cepService.consultar(cep).subscribe({
      next: (dados) => {
        this.consultandoCep = false;
        if (dados.erro || !dados.localidade || !dados.uf || !dados.ibge || !dados.estado) {
          this.erroCep = 'CEP não encontrado. Confira os números e tente novamente.';
          this.changeDetector.markForCheck();
          return;
        }
        this.formGroup.patchValue({
          logradouro: dados.logradouro ?? '',
          bairro: dados.bairro ?? '',
          municipioNome: dados.localidade,
          municipioCodigoIbge: dados.ibge,
          estadoSigla: dados.uf,
          estadoNome: dados.estado,
        });
        this.estadoBusca.setValue({ id: 0, sigla: dados.uf, nome: dados.estado }, { emitEvent: false });
        this.municipioBusca.setValue({ id: Number(dados.ibge), nome: dados.localidade }, { emitEvent: false });
        this.carregarMunicipios(dados.uf);
        this.enderecoAntigo = '';
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.consultandoCep = false;
        this.erroCep = 'Não foi possível consultar o CEP. Verifique a conexão e tente novamente.';
        this.changeDetector.markForCheck();
      },
    });
  }

  salvar(): void {
    if (this.salvando || this.excluindo || this.carregandoFoto()) {
      return;
    }
    if (this.formGroup.invalid || this.estadoBusca.invalid || this.municipioBusca.invalid) {
      this.formGroup.markAllAsTouched();
      this.estadoBusca.markAsTouched();
      this.municipioBusca.markAsTouched();
      return;
    }

    const { id, senha, confirmarSenha, ...cliente } = this.formGroup.getRawValue() as ClienteRequest & { id: number | null; senha?: string; confirmarSenha?: string };
    if (this.modoPublico && senha !== confirmarSenha) {
      this.formGroup.get('confirmarSenha')?.setErrors({ diferente: true });
      this.formGroup.get('confirmarSenha')?.markAsTouched();
      return;
    }
    cliente.cep = cliente.cep?.trim() || null;
    cliente.nome = cliente.nome.trim();
    cliente.email = cliente.email.trim().toLowerCase();
    cliente.logradouro = cliente.logradouro.trim();
    cliente.numero = cliente.numero.trim();
    cliente.bairro = cliente.bairro.trim();
    if (this.fotoBase64) cliente.fotoBase64 = this.fotoBase64;
    if (this.removerFoto) cliente.removerFoto = true;
    const request = this.modoPublico
      ? this.auth.cadastrar(cliente, senha ?? '')
      : this.modoPerfil
      ? this.auth.atualizarMeuCadastro(cliente)
      : id
      ? this.clienteService.editar(id, cliente)
      : this.clienteService.cadastrar(cliente);

    this.salvando = true;
    this.erro = '';
    request.subscribe({
      next: () => {
        if (this.modoPublico) {
          this.auth.entrar(cliente.email, senha ?? '').subscribe({
            next: (usuario) => this.router.navigate([usuario ? '/painel' : '/login']),
            error: () => this.router.navigate(['/login']),
          });
        } else this.router.navigate([this.modoPerfil ? '/painel' : '/clientes']);
      },
      error: (resposta: HttpErrorResponse) => {
        this.salvando = false;
        const api = resposta.error as {
          field?: string;
          message?: string;
          mensagem?: string;
          errors?: { field: string; message: string }[];
        } | null;
        const erros = api?.errors ?? (api?.field && api?.message ? [{ field: api.field, message: api.message }] : []);
        for (const item of erros) {
          const control = this.formGroup.get(item.field);
          control?.setErrors({ ...control.errors, api: item.message });
          control?.markAsTouched();
        }
        const semCampoVisivel = erros.find((item) => !['nome', 'email', 'telefone', 'cpf', 'cep', 'logradouro', 'numero', 'bairro'].includes(item.field));
        this.erro = erros.length
          ? semCampoVisivel?.message ?? 'Confira os campos destacados e tente novamente.'
          : api?.mensagem ?? (resposta.status === 413 ? 'A foto é grande demais para enviar.' : 'Não foi possível salvar o cliente. Tente novamente.');
        this.changeDetector.markForCheck();
      },
    });
  }

  excluir(): void {
    const id = this.formGroup.value.id;
    if (id && !this.salvando && !this.excluindo && confirm('Deseja excluir este cliente?')) {
      this.excluindo = true;
      this.erro = '';
      this.clienteService.excluir(id).subscribe({
        next: () => this.router.navigate(['/clientes']),
        error: () => {
          this.excluindo = false;
          this.erro = 'Não foi possível excluir o cliente. Tente novamente.';
          this.changeDetector.markForCheck();
        },
      });
    }
  }
}
