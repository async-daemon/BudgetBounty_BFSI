export interface UserDto {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  role: string;
  rewardPoints: number;
  initials: string;
  avatarColor: string;
  admin: boolean;
}

export interface AuthResponse {
  token: string;
  user: UserDto;
}
