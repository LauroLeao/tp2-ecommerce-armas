import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { ClienteService } from '../../../services/cliente.service';
import { Cliente } from '../../../models/cliente.model';

@Component({
  imports: [
    MatTableModule,
    MatInputModule,
    MatFormFieldModule,
    MatButtonModule,
    MatIconModule,
    RouterLink,
  ],
  selector: 'app-cliente-list',
  styleUrl: './cliente-list.css',
  templateUrl: './cliente-list.html',
})
export class ClienteList implements OnInit {
  displayedColumns = ['numero', 'nome', 'email', 'telefone', 'endereco', 'acao'];
  dataSource = new MatTableDataSource<Cliente>();
  carregando = false;
  erro = '';
  excluindoId: number | null = null;
  private readonly changeDetector = inject(ChangeDetectorRef);

  constructor(private clienteService: ClienteService) {}

  ngOnInit(): void {
    this.dataSource.filterPredicate = (cliente, filtro) =>
      `${cliente.nome} ${cliente.email}`.toLowerCase().includes(filtro);
    this.carregarClientes();
  }

  iniciais(nome: string): string {
    const partes = nome.trim().split(/\s+/).filter(Boolean);
    return partes.length
      ? `${partes[0][0]}${partes.length > 1 ? partes[partes.length - 1][0] : ''}`.toUpperCase()
      : '?';
  }

  aplicarFiltro(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  excluir(id: number): void {
    if (this.excluindoId !== null || !confirm('Deseja excluir este cliente?')) {
      return;
    }

    this.excluindoId = id;
    this.erro = '';
    this.clienteService.excluir(id).subscribe({
      next: () => {
        this.excluindoId = null;
        this.carregarClientes();
      },
      error: () => {
        this.excluindoId = null;
        this.erro = 'Não foi possível excluir o cliente. Tente novamente.';
        this.changeDetector.markForCheck();
      },
    });
  }

  carregarClientes(): void {
    this.carregando = true;
    this.erro = '';
    this.changeDetector.markForCheck();
    this.clienteService.listar().subscribe({
      next: (clientes) => {
        this.dataSource.data = clientes;
        this.carregando = false;
        this.changeDetector.markForCheck();
      },
      error: () => {
        this.carregando = false;
        this.erro =
          'Não foi possível carregar os clientes. Verifique se o servidor está funcionando.';
        this.changeDetector.markForCheck();
      },
    });
  }
}
