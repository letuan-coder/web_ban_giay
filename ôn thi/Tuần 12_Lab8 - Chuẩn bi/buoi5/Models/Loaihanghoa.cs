using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Xml.Linq;

namespace buoi5.Models
{
    public partial class Loaihanghoa
    {
        public Loaihanghoa()
        {
            Hanghoa = new HashSet<Hanghoa>();
        }
        [Required(ErrorMessage = "Xin nhập mã loại ")]
        [Display(Name = "Mã loại hàng hóa")]
        public string Maloai { get; set; }
        [Required(ErrorMessage = "Xin nhập tên loại ")]
        [Display(Name = "Tên loại hàng hóa")]
        public string Tenloai { get; set; }

        public ICollection<Hanghoa> Hanghoa { get; set; }
    }
}
