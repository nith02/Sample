import React from 'react';
import '../App.css';

class Shop extends React.Component {
    constructor(props) {
        super();
        this.state = {
            name: props.name,
        }

        this.handleEnterShop = this.handleEnterShop.bind(this);
    }

    handleEnterShop(shop) {
        window.location.hash = `#shop/${shop}`;
    }

    render() {
        return (
            <div className="Shop" onClick={() => this.handleEnterShop(this.state.name)}>
                {this.state.name}
            </div>
        )
    }
}

export default Shop;
