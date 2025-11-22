export type StallSize = 'small' | 'medium' | 'large';
export type StallStatus = 'available' | 'reserved' | 'selected';

export interface Stall {
  id: string;
  label: string;
  size: StallSize;
  price: number;
  status: StallStatus;
  row: number;
  col: number;
  reservedBy?: string;
}

export interface Vendor {
  id: string;
  businessName: string;
  contactPerson: string;
  email: string;
  phone: string;
  genres: string[];
}

export interface Reservation {
  id: string;
  vendorId: string;
  stalls: string[];
  totalPrice: number;
  reservationDate: string;
  qrCode: string;
}

// Generate stall grid (10 rows x 8 columns = 80 stalls)
export const generateStalls = (): Stall[] => {
  const stalls: Stall[] = [];
  const sizes: StallSize[] = ['small', 'medium', 'large'];
  const prices = { small: 15000, medium: 25000, large: 40000 };
  
  for (let row = 0; row < 10; row++) {
    for (let col = 0; col < 8; col++) {
      const id = `${String.fromCharCode(65 + row)}${String(col + 1).padStart(2, '0')}`;
      const size = sizes[Math.floor(Math.random() * sizes.length)];
      const isReserved = Math.random() < 0.3; // 30% reserved
      
      stalls.push({
        id,
        label: id,
        size,
        price: prices[size],
        status: isReserved ? 'reserved' : 'available',
        row,
        col,
      });
    }
  }
  
  return stalls;
};

export const mockStalls = generateStalls();

export const availableLiteraryGenres = [
  'Fiction',
  'Poetry',
  'Biography',
  'Children\'s Books',
  'Mystery & Thriller',
  'Science Fiction',
  'Fantasy',
  'Romance',
  'Historical Fiction',
  'Self-Help',
  'Business',
  'Philosophy',
  'Travel',
  'Cookbooks',
  'Art & Photography',
  'Religion & Spirituality',
  'Science & Nature',
  'Comics & Graphic Novels',
  'Young Adult',
  'Literary Criticism',
];
