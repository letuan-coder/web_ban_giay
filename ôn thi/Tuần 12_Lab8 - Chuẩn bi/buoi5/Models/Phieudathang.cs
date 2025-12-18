using System;
using System.Collections.Generic;

namespace buoi5.Models
{
    public partial class Phieudathang
    {
        public Phieudathang()
        {
            Chitietphieudathang = new HashSet<Chitietphieudathang>();
            Phieugiaohang = new HashSet<Phieugiaohang>();
        }

        public string Mapdh { get; set; }
        public DateTime? Ngaydh { get; set; }
        public DateTime? Ngaygh { get; set; }
        public string Diachigh { get; set; }
        public string Makh { get; set; }

        public Khachhang MakhNavigation { get; set; }
        public ICollection<Chitietphieudathang> Chitietphieudathang { get; set; }
        public ICollection<Phieugiaohang> Phieugiaohang { get; set; }
    }
}
