import { Component, EventEmitter, Input, Output } from '@angular/core';
import { PropertyService } from '../services/property.service';

@Component({
  selector: 'app-hero-card',
  templateUrl: './hero-card.component.html',
  styleUrls: ['./hero-card.component.scss']
})
export class HeroCardComponent {
  @Input() hero: any;
  @Input() secondHero: any;
  @Input() selectedProperties!: any[]; 
  @Output() showPropertiesChange = new EventEmitter<any[]>();


  showProperties = this.propertyService.showProperties;

  constructor (private propertyService: PropertyService){}

    onCheckboxChange(property: any) {
      property.checked = !property.checked;
      this.showPropertiesChange.emit(this.showProperties);
    }

}