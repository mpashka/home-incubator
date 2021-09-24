export interface EntityUserEmail {
  email: string,
  confirmed: boolean,
}

export interface EntityUserPhone {
  phone: string,
  confirmed: boolean,
}

export interface EntityUserImage {
  id: number,
  contentType: string|null,
}

export interface EntityUser {
  userId: number,
  firstName: string,
  lastName: string,
  nickName: string,
  primaryImage: EntityUserImage | null,
  phones: Array<EntityUserPhone>,
  emails: Array<EntityUserEmail>,
  images: Array<EntityUserImage>,
}
