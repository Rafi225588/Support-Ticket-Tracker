import { Role } from './enums';

// Backend theke ja ashe (UserResponse)
export interface User {
  id: number;
  name: string;
  email: string;
  role: Role;
}

// Create/Update er somoy ja pathai (UserRequest)
export interface UserRequest {
  name: string;
  email: string;
  role: Role;
}
