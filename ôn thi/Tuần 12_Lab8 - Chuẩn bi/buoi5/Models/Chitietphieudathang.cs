using System;
using System.Collections.Generic;

namespace buoi5.Models
{
    public partial class Chitietphieudathang
    {
        public string Mapdh { get; set; }
        public string Mahang { get; set; }
        public double? Dongia { get; set; }
        public double? Soluong { get; set; }

        public Hanghoa MahangNavigation { get; set; }
        public Phieudathang MapdhNavigation { get; set; }
    }
}
