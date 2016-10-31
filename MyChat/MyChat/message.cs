namespace MyChat
{
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Data.Entity.Spatial;

    [Table("mychat.messages")]
    public partial class message
    {
        public int MessageId { get; set; }

        [StringLength(1073741823)]
        public string Text { get; set; }

        public int? User_UserId { get; set; }
    }
}
