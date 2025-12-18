using System;
using System.Collections.Generic;

namespace buoi5.Models
{
    public partial class Phieugiaohang
    {
        public Phieugiaohang()
        {
            Chitietphieugiaohang = new HashSet<Chitietphieugiaohang>();
        }

        public string Mapgh { get; set; }
        public DateTime? Ngaygh { get; set; }
        public string Diachigh { get; set; }
        public string Tennguoinhan { get; set; }
        public string Mapdh { get; set; }
        public string Manv { get; set; }

        public Nhanvien ManvNavigation { get; set; }
        public Phieudathang MapdhNavigation { get; set; }
        public ICollection<Chitietphieugiaohang> Chitietphieugiaohang { get; set; }
    }
}
