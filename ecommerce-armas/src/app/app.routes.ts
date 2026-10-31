import { Routes } from '@angular/router';
import { ProdutoList } from './components/produtos/produto-list/produto-list';
import { ProdutoForm } from './components/produtos/produto-form/produto-form';
import { produtoResolver } from './resolvers/produto-resolver';
import { ClienteList } from './components/clientes/cliente-list/cliente-list';
import { ClienteForm } from './components/clientes/cliente-form/cliente-form';
import { clienteResolver } from './resolvers/cliente-resolver';
import { CadastroAdmin } from './components/cadastros/cadastro-admin';
import { PedidoAdmin } from './components/pedidos/pedido-admin';
import { Login } from './components/acesso/login';
import { Painel } from './components/painel/painel';
import { exigirPerfil, redirecionarPainel } from './services/auth.guard';
import { meuClienteResolver } from './resolvers/meu-cliente-resolver';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'painel'
  },
  { path: 'login', component: Login, canActivate: [redirecionarPainel], title: 'Entrar' },
  { path: 'cadastro', component: ClienteForm, canActivate: [redirecionarPainel], title: 'Criar conta' },
  { path: 'painel', component: Painel, canActivate: [exigirPerfil()], title: 'Painel inicial' },
  { path: 'perfil', component: ClienteForm, canActivate: [exigirPerfil('CLIENTE')], resolve: { cliente: meuClienteResolver }, title: 'Meu perfil' },
  {
    path: 'produtos',
    component: ProdutoList,
    canActivate: [exigirPerfil('ADMIN')],
    title: 'Lista de Produtos'
  },
  {
    path: 'produtos/new',
    component: ProdutoForm,
    canActivate: [exigirPerfil('ADMIN')],
    title: 'Novo Produto'
  },
  {
    path: 'produtos/edit/:id',
    component: ProdutoForm,
    canActivate: [exigirPerfil('ADMIN')],
    title: 'Editar Produto',
    resolve: {
      produto: produtoResolver
    }
  },
  {
    path: 'clientes',
    component: ClienteList,
    canActivate: [exigirPerfil('ADMIN')],
    title: 'Lista de Clientes'
  },
  {
    path: 'clientes/new',
    component: ClienteForm,
    canActivate: [exigirPerfil('ADMIN')],
    title: 'Novo Cliente'
  },
  {
    path: 'clientes/edit/:id',
    component: ClienteForm,
    canActivate: [exigirPerfil('ADMIN')],
    title: 'Editar Cliente',
    resolve: {
      cliente: clienteResolver
    }
  },
  { path: 'categorias', component: CadastroAdmin, canActivate: [exigirPerfil('ADMIN')], data: { tipo: 'categorias' }, title: 'Categorias' },
  { path: 'marcas', component: CadastroAdmin, canActivate: [exigirPerfil('ADMIN')], data: { tipo: 'marcas' }, title: 'Marcas e Modelos' },
  { path: 'modelos', redirectTo: 'marcas' },
  { path: 'fornecedores', component: CadastroAdmin, canActivate: [exigirPerfil('ADMIN')], data: { tipo: 'fornecedores' }, title: 'Fornecedores' },
  { path: 'pedidos', component: PedidoAdmin, canActivate: [exigirPerfil('ADMIN')], title: 'Pedidos de clientes' },
  {
    path: '**',
    redirectTo: 'painel'
  }
];
