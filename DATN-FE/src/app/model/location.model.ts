export interface Province {
  code: string;
  nameWithType: string;
}

export interface District {
  code: string;
  nameWithType: string;
  parent_code: string;
}

export interface Commune {
  code: string;
  nameWithType: string;
  parent_code: string;
  pathWithType:string;
}
