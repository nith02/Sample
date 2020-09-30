import React from 'react';
import { Link } from 'react-router-dom';
import { context } from '../context';
import Camera from '../components/Camera';
import '../App.css';

class Shop extends React.Component {
    static contextType = context;

    constructor() {
        super();
        this.state = {
            shopName: "",
        }
    }

    render() {
        const { shop_list } = this.context;

        return (
            <div className="App">
                <header className="App-header">
                    <div className="Toolbar">
                        <Link className="HomeButton" to={`/`}>&lt;</Link>
                        {this.props.match.params.shop}
                        <Link className="AddButton" to={`/shop/${this.props.match.params.shop}/add`}>+</Link>
                    </div>
                    {shop_list.map((shop) => {
                        if (shop.name === this.props.match.params.shop) {
                            return shop.cameras.map((camera) => {
                                return (<Camera name={camera} shop={this.props.match.params.shop} />);
                            })
                        }
                    })}
                </header>
            </div>
        )
    }
}

export default Shop;