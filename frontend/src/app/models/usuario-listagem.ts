export interface UsuarioListagem {
  id: number;
  nome: string;
  email: string;
  role: 'USER' | 'ADMIN';
}
